package org.lab1.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.lab1.bean.data.Identable;
import org.lab1.data.entity.enums.TicketType;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.*;
import java.util.Date;

@Entity
@Table(name = "tickets")
@Setter
@SqlResultSetMapping(
        name = "ticketsMapping",
        entities = @EntityResult(
                entityClass = Ticket.class,
                fields = {
                        @FieldResult(name = "id", column = "id"),
                        @FieldResult(name = "name", column = "name"),
                        @FieldResult(name = "coordinates", column = "coordinates_id"),
                        @FieldResult(name = "creationDate", column = "creation_date"),
                        @FieldResult(name = "person", column = "person_id"),
                        @FieldResult(name = "event", column = "event_id"),
                        @FieldResult(name = "price", column = "price"),
                        @FieldResult(name = "type", column = "type"),
                        @FieldResult(name = "discount", column = "discount"),
                        @FieldResult(name = "number", column = "number"),
                        @FieldResult(name = "venue", column = "venue_id"),
                        @FieldResult(name = "owner", column = "user_id")
                }
        )
)
@XmlRootElement(name = "ticket")
@XmlAccessorType(XmlAccessType.FIELD)
public class Ticket implements Identable, Ownerable {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
    private Long id; // Must be greater than 0, unique, and generated automatically

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "Name cannot be empty")
    @XmlElement
    private String name; // Cannot be null, string cannot be empty

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "coordinates_id", nullable = false)
    @XmlElement
    private Coordinates coordinates; // Cannot be null

    @Transient
    @Getter
    private Long coordinatesId;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @XmlTransient
    private Date creationDate; // Cannot be null, generated automatically

    @Column(nullable = false)
    @XmlElement
    private double price; // Must be greater than 0

    @Column(nullable = false)
    @Max(value = 100)
    @XmlElement
    private int discount; // Must be greater than 0 and less than 100

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @XmlElement
    private TicketType type; // Cannot be null

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "venue_id", nullable = false)
    @XmlElement
    private Venue venue; // Cannot be null

    @Transient
    @Getter
    private Long venueId;

    @Column(name = "number")
    @XmlElement
    private Integer number = 1; // Must be greater than 0

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "event_id", nullable = false)
    @XmlElement
    private Event event; // Cannot be null

    @Transient
    @Getter
    private Long eventId;

    @Setter
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "person_id")
    @XmlElement
    private Person person;

    @Transient
    @Getter
    private Long personId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @XmlTransient
    private User owner; // Cannot be null


    @PrePersist
    public void onPrePersist() {
        this.creationDate = new Date();
    }


    public void setName(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name cannot be null or empty");
        this.name = name;
    }


    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) throw new IllegalArgumentException("Coordinates cannot be null");
        this.coordinates = coordinates;
    }


    public void setPrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than 0");
        this.price = price;
    }


    public void setTicketType(TicketType type) {
        if (type == null) throw new IllegalArgumentException("Ticket type cannot be null");
        this.type = type;
    }


    public void setDiscount(int discount) {
        if (discount < 0 || discount > 100)
            throw new IllegalArgumentException("Discount must be greater than 0 and less than 101");
        this.discount = discount;
    }

    public void setNumber(Integer number) {
        if (number == null || number <= 0)
            throw new IllegalArgumentException("Number must be greater than 0 and cannot be null");
        this.number = number;
    }

    public void setVenue(Venue venue) {
        if (venue == null) throw new IllegalArgumentException("Venue cannot be null");
        this.venue = venue;
    }

    public void setEvent(Event event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public TicketType getType(){
        return type;
    }

    public Integer getNumber(){
        return number;
    }

    public int getDiscount(){
        return discount;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    public Event getEvent() {
        return event;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Venue getVenue() {
        return venue;
    }

    public Person getPerson() {
        return person;
    }

    public Long getPassedEventId() {
        return eventId;
    }

    public Long getPassedPersonId() {
        return personId;
    }

    public Long getPassedCoordinatesId() {
        return coordinatesId;
    }

    public Long getPassedVenueId() {
        return venueId;
    }

    @Override
    public long getId() {
        return id.longValue();
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getCreationDate() {
        return creationDate;
    }
}