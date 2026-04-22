package com.hrlee.transnaviserver.springboot.repository.jpa;

import com.hrlee.transnaviserver.springboot.entity.jpa.GradeComment;
import com.hrlee.transnaviserver.springboot.entity.jpa.GradeCommentPK;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Deprecated
public interface GradeCommentRepository extends JpaRepository<GradeComment, GradeCommentPK> {

    @Query(value = "select count(poi_id) from grade_comment where poi_id=:poiId", nativeQuery = true)
    public Integer getCountByPoiId(@Param("poiId") String poiId);

    @Query(value = "select avg(grade) from grade_comment where poi_id=:poiId", nativeQuery = true)
    public Float getAvgByPoiId(@Param("poiId") String poiId);

    @Query(value = "select * from grade_comment where poi_id=:poiId", nativeQuery = true)
    public List<GradeComment> getCommentByPoiId(@Param("poiId") String poiId);

    @Query(value = "select * from grade_comment where poi_id=:poiId and usr_id=:usrId", nativeQuery = true)
    public GradeComment getCommentByUsrIdAndPoiId(@Param("usrId") String usrId, @Param("poiId") String poiId);

    @Modifying
    @Transactional
    @Query(value = "insert into grade_comment values ( " +
            "(select usr_id from user_registered where usr_id=:usrId), " +
            ":poiId, :grade, :comment)", nativeQuery = true)
    public void addComment(@Param("usrId") String usrId,
                           @Param("poiId") String poiId,
                           @Param("grade") int grade,
                           @Param("comment") String comment);

    @Modifying
    @Transactional
    @Query(value = "delete from grade_comment where usr_id=:usrId and poi_id=:poiId", nativeQuery = true)
    public void removeComment(@Param("usrId") String usrId, @Param("poiId") String poiId);

    @Modifying
    @Transactional
    @Query(value = "update grade_comment set grade=:grade, comment=:comment " +
            "where poi_id=:poiId and usr_id=:usrId",
            nativeQuery = true)
    public void editComment(@Param("comment") String comment,
                            @Param("grade") float grade,
                            @Param("poiId") String poiId,
                            @Param("usrId") String usrId);

}

