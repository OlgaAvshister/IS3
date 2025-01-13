package org.lab1.data.entity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.lab1.data.entity.enums.Color;
import org.lab1.bean.data.Identable;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Person implements Identable, Ownerable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Primary key, auto-generated

    @Column
    private Color eyeColor;

    @Column(nullable = false)
    private Color hairColor;

    @OneToOne(cascade = CascadeType.MERGE)
//    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Transient
    private Long locationId;

    @Column(nullable = false)
    private int height = 1;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner; // Cannot be null

    @Override
    public long getId() {
        return id;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Long getPassedLocationId() {
        return locationId;
    }
}
