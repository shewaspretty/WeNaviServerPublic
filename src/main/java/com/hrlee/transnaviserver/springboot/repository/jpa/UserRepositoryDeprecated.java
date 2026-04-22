package com.hrlee.transnaviserver.springboot.repository.jpa;

import com.hrlee.transnaviserver.springboot.entity.jpa.UserRegistered;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Deprecated
public interface UserRepositoryDeprecated extends JpaRepository<UserRegistered, Long> {

    @Modifying
    @Transactional
    @Query(value = "insert into user_registered values (:usrId, :password, :nickName)", nativeQuery = true)
    public void addNewUser(@Param("usrId") String usrId, @Param("password") String password,
                           @Param("nickName") String nickName);

    @Query(value = "select * from user_registered where usr_id=:usrId", nativeQuery = true)
    public UserRegistered getUserById(@Param("usrId") String usrId);

    @Modifying
    @Transactional
    @Query(value = "update user_registered set nick_name=:nickName where usr_id=:usrId", nativeQuery = true)
    public void editUserNickname(@Param("nickName") String nickName, @Param("usrId") String usrId);

    @Query(value = "select nick_name from user_registered where usr_id=:usrId", nativeQuery = true)
    public String getUserNickname(@Param("usrId") String usrId);
}
