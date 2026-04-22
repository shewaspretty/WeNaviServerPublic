package com.hrlee.transnaviserver.springboot.entity.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Deprecated
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class GradeCommentPK implements Serializable {
    private String usrId;
    private String poiId;

    public GradeCommentPK(String usrId, String poiId) {
        this.poiId = poiId;
        this.usrId = usrId;
    }
}
