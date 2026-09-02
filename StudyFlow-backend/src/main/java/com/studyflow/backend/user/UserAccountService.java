package com.studyflow.backend.user;

import com.studyflow.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount getRequired(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "User was not found"));
    }

    public UserAccount getRequiredForFollow(Long userId) {
        return userAccountRepository.findByIdForFollow(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "User was not found"));
    }

    @Transactional
    public void changeStatus(Long userId, UserStatus status) {
        getRequired(userId).changeStatus(status);
    }
}
