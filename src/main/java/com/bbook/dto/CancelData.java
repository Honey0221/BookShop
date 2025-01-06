package com.bbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelData {
    private String imp_uid;
    private boolean checksum;

    public CancelData(String imp_uid, boolean checksum) {
        this.imp_uid = imp_uid;
        this.checksum = checksum;
    }
}