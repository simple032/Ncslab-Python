package com.ncslab.database;

import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "mdl_block")
@Getter
public class MdlBlock implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "library_id")
    private Integer libraryId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "type")
    private String type;

    @Column(name = "data")
    @Lob
    private String data;

    @Column(name = "logic")
    @Lob
    private String logic;

    @Column(name = "markup")
    private String markup;

    @Column(name = "config")
    private String config;

    @Column(name = "plant_id")
    private Integer plantId;

    @Column(name = "public")
    private Integer publicFlag;

    @Column(name = "icon")
    @Lob
    private String icon;

    @Column(name = "last_update")
    private String lastUpdate;


    // hashCode, equals and toString methods
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MdlBlock that = (MdlBlock) obj;
        return id.equals(that.id);
    }

    @Override
    public String toString() {
        return "BlockEntity{" +
            "id=" + id +
            ", libraryId=" + libraryId +
            ", userId='" + userId + '\'' +
            ", order=" + order +
            ", type='" + type + '\'' +
            ", data='" + data + '\'' +
            ", logic='" + logic + '\'' +
            ", markup='" + markup + '\'' +
            ", config='" + config + '\'' +
            ", plantId=" + plantId +
            ", publicFlag=" + publicFlag +
            ", icon='" + icon + '\'' +
            ", lastUpdate='" + lastUpdate + '\'' +
            '}';
    }
}
