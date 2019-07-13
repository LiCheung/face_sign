package com.twelve.model.login;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wang0 on 2016/9/21.
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "roles")
public class Roles{


    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String mark;

    @ManyToMany()
    @JoinTable(name = "role_member",joinColumns = {@JoinColumn(name = "role_id")}
            ,inverseJoinColumns ={@JoinColumn(name = "member_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Member> members = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
}
