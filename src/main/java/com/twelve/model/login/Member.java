package com.twelve.model.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "member")
@Setter
@Getter
public class Member implements UserDetails { //1

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Integer id;
    private Long stuId;

    @Column(name = "names")
    private String name;
    private Integer grade;
    private Integer isstart;
    private String pwd;
    private String picAddr;

    @ManyToMany(mappedBy = "members")
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<Roles> roles = new ArrayList<Roles>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> auths = new ArrayList<SimpleGrantedAuthority>();
        List<Roles> roles= this.getRoles();
        for(Roles role:roles){
            auths.add(new SimpleGrantedAuthority(role.getMark()));
        }
        return auths;
    }

    @Override
    public String getPassword() {
        return pwd;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getStuId() {
        return stuId;
    }

    public void setStuId(Long stuId) {
        this.stuId = stuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getIsstart() {
        return isstart;
    }

    public void setIsstart(Integer isstart) {
        this.isstart = isstart;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPicAddr() {
        return picAddr;
    }

    public void setPicAddr(String picAddr) {
        this.picAddr = picAddr;
    }

    public List<Roles> getRoles() {
        return roles;
    }

    public void setRoles(List<Roles> roles) {
        this.roles = roles;
    }
}
