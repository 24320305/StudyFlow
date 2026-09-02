package com.studyflow.backend.user;

import com.studyflow.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final UserAccountRepository userAccountRepository;

    public ProfileService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserProfileResponse get(Long userId) {
        return UserProfileResponse.from(getRequired(userId));
    }

    @Transactional
    public UserProfileResponse update(Long userId, UpdateProfileRequest request) {
        if (request.nickname() == null && request.avatarUrl() == null) {
            throw BusinessException.badRequest("EMPTY_PROFILE_UPDATE", "Provide a nickname or avatarUrl to update the profile");
        }
        if (request.nickname() != null && request.nickname().trim().isEmpty()) {
            throw BusinessException.badRequest("INVALID_NICKNAME", "nickname cannot be blank");
        }
        if (request.avatarUrl() != null && request.avatarUrl().trim().isEmpty()) {
            throw BusinessException.badRequest("INVALID_AVATAR_URL", "avatarUrl cannot be blank");
        }
        UserAccount user = getRequired(userId);
        user.updateProfile(
                request.nickname() == null ? null : request.nickname().trim(),
                request.avatarUrl() == null ? null : request.avatarUrl().trim());
        return UserProfileResponse.from(user);
    }

    private UserAccount getRequired(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "User was not found"));
    }
}
