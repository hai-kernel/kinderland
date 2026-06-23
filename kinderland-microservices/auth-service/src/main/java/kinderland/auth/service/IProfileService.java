package kinderland.auth.service;

import jakarta.transaction.Transactional;
import kinderland.auth.model.dto.request.ChangePasswordRequest;
import kinderland.auth.model.dto.request.ProfileUpdateRequest;
import kinderland.auth.model.dto.response.UserResponse;

public interface IProfileService {
    @Transactional
    void changePassword(ChangePasswordRequest request);

    UserResponse getProfile();

    void updateProfile(ProfileUpdateRequest profile);
}
