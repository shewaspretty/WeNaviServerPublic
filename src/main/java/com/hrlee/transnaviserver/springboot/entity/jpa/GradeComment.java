package com.hrlee.transnaviserver.springboot.entity.jpa;

import com.hrlee.transnaviserver.springboot.dto.rest.rate.RatingRequest;
import jakarta.persistence.*;
import lombok.Getter;

@Deprecated
@Entity
@Getter
@IdClass(GradeCommentPK.class)
public class GradeComment {
    @Id
    @ManyToOne(targetEntity = UserRegistered.class)
    @JoinColumn(name = "usr_id")
    private UserRegistered usrId;

    @Id
    @Column(name = "poi_id", nullable = false)
    private String poiId;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Column(name = "comment")
    private String comment;


    protected GradeComment() {}

    public GradeComment(RatingRequest request, UserRegistered user, String poiId) {
        this.usrId = user;
        this.poiId = poiId;
        this.grade = request.getGrade();
        this.comment = request.getComment();
    }



}
