package com.laanto.it.paycenter.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 统一定义id和version的domain基类.
 * <p>
 * 基类统一定义id的属性名称、数据类型、列名映射及生成策略.
 * Oracle需要每个Entity独立定义id的SEQUCENCE时，不继承于本类而改为实现一个Idable的接口。
 *
 * @author Chen, Huimin
 */
@MappedSuperclass
public abstract class BaseDomain implements Serializable {
    private static final long serialVersionUID = 2931563655656530260L;
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    /**
     * 版本号
     */
    @Version
    @JsonIgnore
    @Column(nullable = false)
    protected Long version;

    /**
     * 删除状态
     */
    @JsonIgnore
    protected boolean deleted;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "add_time", nullable = false)
    protected Date addTime = new Date();// 添加时间，这里为长时间格式

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time")
    protected Date updateTime;// 添加时间，这里为长时间格式

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
