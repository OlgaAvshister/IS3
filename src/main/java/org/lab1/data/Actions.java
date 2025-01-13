package org.lab1.data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.hibernate.cfg.Configuration;
import org.lab1.data.entity.*;

public class Actions {
    static String persistenceName = "default";
    static final Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("default", configuration.getProperties());

    private Actions(){
    }
    
    @SuppressWarnings("unused")
    private static final Map<String, String> classMap = createClassMap();

    private static Map<String, String> createClassMap() {
        Map<String, String> map = new HashMap<>();
        map.put(Address.class.getName(), "address_id");
        map.put(Coordinates.class.getName(), "coordinates_id");
        map.put(Event.class.getName(), "event_id");
        map.put(Location.class.getName(), "location_id");
        map.put(Person.class.getName(), "person_id");
        map.put(Venue.class.getName(), "venue_id");
        return map;
    }

    public static void update(Object o) {
        Change change = new Change();
        change.setEntity(o.getClass().getName());
        change.setOwner(null);
        change.setType("Update");
        change.setChangeDate(new Date());
        add(change);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(o);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static void addMain(Object o){
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(o);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally {
            em.close();
        }
    }

    public static void add(Object o){
        if (!o.getClass().equals(Change.class) && !o.getClass().equals(Import.class)){
            Change change = new Change();
            change.setEntity(o.getClass().getName());
            change.setOwner(null);
            change.setType("Add");
            change.setChangeDate(new Date());
            add(change);
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(o);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally {
            em.close();
        }
    }

    public static void addCommit(Object o){
        if (!o.getClass().equals(Change.class) && !o.getClass().equals(Import.class)){
            Change change = new Change();
            change.setEntity(o.getClass().getName());
            change.setOwner(null);
            change.setType("Add");
            change.setChangeDate(new Date());
            addCommit(change);
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.merge(o);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
        finally {
            em.close();
        }
    }


    public static void delete(Object o) {
        Change change = new Change();
        change.setEntity(o.getClass().getName());
        change.setOwner(null);
        change.setType("Delete");
        change.setChangeDate(new Date());
        add(change);
        EntityManager em = emf.createEntityManager();
        System.out.println("Attempting to remove item: " + o);
        try {
            em.getTransaction().begin();
            em.remove(em.merge(o));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.err.println("Error during deletion: " + e.getMessage());
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static <T> T find(Class<T> classname, long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        T res = em.find(classname, id);
        em.getTransaction().commit();
        em.close();
        return res;
    }

    public static User getUserByLogin(String login) {
        List<User> users = findAll(User.class);
        Optional<User> res = users.stream().filter(u -> u.getLogin().equals(login)).findFirst();
        return res.orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> findAll(Class<T> classname) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        List<T> res = em.createQuery("select o from " + classname.getName() + " o order by o.id").getResultList();
        em.getTransaction().commit();
        em.close();
        
        return res;
    }

    @SuppressWarnings("unused")
    private static void refresh(){
        emf.getCache().evictAll();
    }

    public static int getNumberOfLessVenueTickets(int minVenue) {
        EntityManagerFactory emfl = Persistence.createEntityManagerFactory(persistenceName);
        EntityManager em = emfl.createEntityManager();
        int res;
        try {
            em.getTransaction().begin();
            res = ((Number) em.createNativeQuery("SELECT get_number_of_less_venue_tickets(" + minVenue + ")")
                    .getSingleResult()).intValue();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
            emfl.close();
        }
        return res;
    }

    public static int getNumberOfMoreNumberTickets(int maxNumber) {
        EntityManagerFactory emfl = Persistence.createEntityManagerFactory(persistenceName);
        EntityManager em = emfl.createEntityManager();
        int res;
        try {
            em.getTransaction().begin();
            res = ((Number) em.createNativeQuery("SELECT get_number_of_more_number_tickets(" + maxNumber + ")")
                    .getSingleResult()).intValue();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
            emfl.close();
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public static List<Ticket> getTicketsWithGreaterNumber(int maxNumber) {
        EntityManagerFactory emfl = Persistence.createEntityManagerFactory(persistenceName);
        EntityManager em = emfl.createEntityManager();
        List<Ticket> res;
        try {
            em.getTransaction().begin();
            String sql = String.format("SELECT * FROM get_tickets_with_greater_number(%d)", maxNumber);
            res = em.createNativeQuery(sql, "ticketsMapping").getResultList();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
            emfl.close();
        }
        return res;
    }

    public static void copyVIPTicket(int id) {
        EntityManagerFactory emfl = Persistence.createEntityManagerFactory(persistenceName);
        EntityManager em = emfl.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("CALL copy_vip_ticket(" + id + ");").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
            emfl.close();
        }
    }

    public static void copyDiscountTicket(int id, int discount) {
        EntityManagerFactory emfl = Persistence.createEntityManagerFactory(persistenceName);
        EntityManager em = emfl.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("CALL copy_discount_ticket(" + id + ", " + discount + ");").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
            emfl.close();
        }
    }


    public static <T> List<Ticket> findTicketByClassId(Class<T> classname, long id) {
        return findAll(Ticket.class).stream()
                .filter(ticket -> {
                    if (Coordinates.class.equals(classname)) {
                        return ticket.getCoordinates().getId() == id;
                    } else if (Venue.class.equals(classname)) {
                        return ticket.getVenue().getId() == id;
                    } else if (Event.class.equals(classname)) {
                        return ticket.getEvent().getId() == id;
                    } else if (Person.class.equals(classname)){
                        return ticket.getPerson().getId() == id;
                    }
                    return false;
                })
                .collect(Collectors.toList());
            }

    public static <T> List<Venue> findVenueByClassId(Class<T> classname, long id) {
        return findAll(Venue.class).stream()
                .filter(venue -> {
                    if (Address.class.equals(classname)) {
                        return venue.getAddress().getId() == id;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public static <T> List<Person> findPersonByClassId(Class<T> classname, long id) {
        return findAll(Person.class).stream()
                .filter(person -> {
                    if (Location.class.equals(classname)) {
                        return person.getLocation().getId() == id;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }


    public static <T> List<T> getAllByUser(Class<T> classname, String login) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        List<T> res = em.createQuery("select o from " + classname.getName() + " o where o.owner.login = :login").
                setParameter("login", login).getResultList();
        em.getTransaction().commit();
        em.close();
        return res;
    }

    private long countAllImports() {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT COUNT(i) FROM Import i", Long.class).getSingleResult();
    }

    private long countAllImportsByUser(String login) {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT COUNT(i) FROM Import i WHERE i.user.login = :login", Long.class)
                .setParameter("login", login)
                .getSingleResult();
    }
}
