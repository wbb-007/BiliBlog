package com.blogbili.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "site_setting")
public class SiteSettingEntity {

    @Id
    @Column(name = "setting_key", length = 80)
    private String key;

    @Lob
    @Column(name = "setting_value", nullable = false, columnDefinition = "LONGTEXT")
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
