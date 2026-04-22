package com.hrlee.transnaviserver.springboot.service.user;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.rest.ErrorResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.user.nickname.UserNicknameEditRequest;
import com.hrlee.transnaviserver.springboot.dto.rest.user.register.UserRegisterRequest;
import com.hrlee.transnaviserver.springboot.dto.rest.user.register.UserRegisterResponse;
import com.hrlee.transnaviserver.springboot.entity.User;
import com.hrlee.transnaviserver.springboot.repository.jdbc.UserRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements LoggAble {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserRegisterResponse.ErrorCode registerNewUser(UserRegisterRequest request) {
        if(request.isCorrupt())
            return UserRegisterResponse.ErrorCode.ERROR;

        String passwordEncrypted = bCryptPasswordEncoder.encode(request.getPassword());
        try {
            if(jdbcTemplate.update("INSERT INTO user VALUES( \"" + request.getId() + "\" , \"" + passwordEncrypted + "\" , \"익명\") ") == 1)
                return UserRegisterResponse.ErrorCode.SUCCESS;
        } catch (DataAccessException e) {
            if(e instanceof DuplicateKeyException)
                return UserRegisterResponse.ErrorCode.DUPLICATED;
            getLogger().error(e + "while adding user");
        }
        return UserRegisterResponse.ErrorCode.ERROR;
    }

    public ErrorResponse editUserNickname(UserNicknameEditRequest request) {
        if(request.getNickname() == null)
            return new ErrorResponse("닉네임 공백은 금지됩니다.");

        String usrIdFound = getUserIdInCurrentSecurityContext();
        if(usrIdFound == null)
            return new ErrorResponse("인증 오류");

        try {
            if(jdbcTemplate.update("UPDATE user SET nickname=\"" + request.getNickname() + "\" WHERE id=\"" + usrIdFound + "\"") == 1)
                return new ErrorResponse(null);

            return new ErrorResponse("동일한 닉네임 입니다.");
        } catch (DataAccessException e) {
            getLogger().error(e + "while editing nickname");
            return new ErrorResponse("인증 오류");
        }
    }

    @Nullable
    public String getUserNickname() {
        String usrIdFound = getUserIdInCurrentSecurityContext();
        if(usrIdFound == null)
            return null;

        User usrFromDB = userRepository.getUser(usrIdFound);
        if(usrFromDB == null)
            return null;

        return usrFromDB.getNickname();
    }

    @Nullable
    public String getUserIdInCurrentSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null)
            return null;
        return authentication.getName();
    }

}
