package org.lab1.data.entity;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lab1.bean.data.Identable;

@Entity
@Table(name = "coordinates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Coordinates implements Identable, Ownerable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Primary key, auto-generated

    @Column(nullable = false)
    @Max(value = 755, message = "Value must be less than 756")
    private Float x = 0F; // Cannot be null

    @Column()
    @Max(value = 685, message = "Value must be less than 686")
    private int y;

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
}