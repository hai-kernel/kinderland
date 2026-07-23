package kinderland.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import kinderland.auth.event.UserCreatedEvent;
import kinderland.auth.model.dto.response.UserResponse;
import kinderland.auth.model.entity.Account;

import java.util.List;

/**
 * Map Account -> UserResponse / UserCreatedEvent. MapStruct tự sinh implementation lúc compile.
 * - role (enum Account.Role) -> String: MapStruct map enum sang tên hằng tự động.
 * - active (boolean): khớp nhờ getter isActive()/field active ở DTO.
 * unmappedTargetPolicy=ERROR: thiếu map field nào sẽ FAIL build (bắt lỗi sớm).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper {

    // authProvider là cột thêm sau nên hàng cũ vẫn NULL — trả "LOCAL" để FE không
    // phải đoán, khớp đúng với Account.isPasswordLoginEnabled().
    @Mapping(
            target = "authProvider",
            expression = "java(account.getAuthProvider() == null "
                    + "? kinderland.auth.model.entity.Account.AuthProvider.LOCAL.name() "
                    + ": account.getAuthProvider().name())"
    )
    UserResponse toUserResponse(Account account);

    List<UserResponse> toUserResponseList(List<Account> accounts);

    // occurredAt do service đóng dấu thời điểm publish (Instant.now()).
    @Mapping(target = "occurredAt", ignore = true)
    UserCreatedEvent toUserCreatedEvent(Account account);
}
