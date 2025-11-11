package com.example.fieldcard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "application_group")
public class ApplicationGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @Column(name = "group_id", nullable = false, unique = true)
   private Long groupId;
   @Column(name = "name", nullable = false)
   private  String name;
   @Column(name="is_active", nullable = false)
   private boolean isActive = true;

   public ApplicationGroup() {
    }
    public ApplicationGroup(Long groupId, String name) {
        this.groupId = groupId;
        this.name = name;
        this.isActive = true;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getGroupId() {
        return groupId;
    }
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    @Override
    public String toString(){
        return "ApplicationGroup{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }

}
