package com.sdlcplatform.service.impl;

import com.sdlcplatform.dto.request.AssignRolesRequest;
import com.sdlcplatform.dto.request.CreateUserRequest;
import com.sdlcplatform.dto.request.UpdateUserRequest;
import com.sdlcplatform.dto.request.UserSkillRequest;
import com.sdlcplatform.dto.response.PagedResponse;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.dto.response.UserSkillResponse;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.entity.UserSkill;
import com.sdlcplatform.exception.EmailAlreadyExistsException;
import com.sdlcplatform.exception.InvalidOperationException;
import com.sdlcplatform.exception.ResourceNotFoundException;
import com.sdlcplatform.mapper.UserMapper;
import com.sdlcplatform.repository.RoleRepository;
import com.sdlcplatform.repository.UserRepository;
import com.sdlcplatform.repository.UserSkillRepository;
import com.sdlcplatform.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSkillRepository userSkillRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public PagedResponse<UserResponse> listUsers(String search, String department, Boolean active, Pageable pageable) {
        Specification<User> spec = buildSearchSpecification(search, department, active);
        Page<UserResponse> page = userRepository.findAll(spec, pageable).map(userMapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment())
                .jobTitle(request.getJobTitle())
                .active(true)
                .emailVerified(true)
                .roles(roles)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);
        applyProfileUpdate(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(UUID id, String currentUserEmail) {
        User user = findUserOrThrow(id);

        if (user.getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new InvalidOperationException("You cannot deactivate your own account");
        }

        user.setActive(false);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse reactivateUser(UUID id) {
        User user = findUserOrThrow(id);
        user.setActive(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse assignRoles(UUID id, AssignRolesRequest request) {
        User user = findUserOrThrow(id);
        user.setRoles(resolveRoles(request.getRoles()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getOwnProfile(String currentUserEmail) {
        return userMapper.toResponse(findUserByEmailOrThrow(currentUserEmail));
    }

    @Override
    @Transactional
    public UserResponse updateOwnProfile(String currentUserEmail, UpdateUserRequest request) {
        User user = findUserByEmailOrThrow(currentUserEmail);
        applyProfileUpdate(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<UserSkillResponse> getSkills(UUID userId) {
        findUserOrThrow(userId);
        return userMapper.toSkillResponseList(userSkillRepository.findByUserId(userId));
    }

    @Override
    @Transactional
    public List<UserSkillResponse> replaceOwnSkills(String currentUserEmail, List<UserSkillRequest> skills) {
        User user = findUserByEmailOrThrow(currentUserEmail);

        userSkillRepository.deleteAllByUserId(user.getId());

        List<UserSkill> newSkills = new ArrayList<>();
        for (UserSkillRequest request : skills) {
            String proficiency = StringUtils.hasText(request.getProficiency())
                    ? request.getProficiency().toUpperCase(Locale.ROOT)
                    : UserSkill.Proficiency.INTERMEDIATE.name();

            newSkills.add(UserSkill.builder()
                    .user(user)
                    .skillName(request.getSkillName())
                    .proficiency(proficiency)
                    .build());
        }

        return userMapper.toSkillResponseList(userSkillRepository.saveAll(newSkills));
    }

    private void applyProfileUpdate(User user, UpdateUserRequest request) {
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name.toUpperCase(Locale.ROOT))
                        .orElseThrow(() -> new ResourceNotFoundException("Unknown role: " + name)))
                .collect(Collectors.toSet());
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Specification<User> buildSearchSpecification(String search, String department, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), likePattern),
                        cb.like(cb.lower(root.get("email")), likePattern)
                ));
            }
            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.toLowerCase(Locale.ROOT)));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}