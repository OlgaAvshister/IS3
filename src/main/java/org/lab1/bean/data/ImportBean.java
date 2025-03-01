package org.lab1.bean.data;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.lab1.bean.data.abstracts.UsedManagerBean;
import org.lab1.data.Actions;
import org.lab1.data.ValidationResult;
import org.lab1.data.entity.Import;
import org.lab1.data.entity.Ticket;
import org.lab1.data.entity.User;
import org.lab1.data.entity.enums.ImportStatus;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.persistence.*;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ManagedBean(name = "importBean")
@RequestScoped
@Getter
@Setter
public class ImportBean extends UsedManagerBean<Import> {
    private String itemName = "import";
    private UploadedFile xmlFile;
    User user = getCurrentOwner();
    private Import currentImport = new Import();
    @Setter
    @Getter(value = AccessLevel.NONE)
    private List<Import> imports;
    TicketLoader loader;
    FacesContext facesContext = FacesContext.getCurrentInstance();
    private MinioClient minioClient;

    public ImportBean() {
        super(Import.class, "import");
        System.out.println("Constructor ImportBean() called");
        this.minioClient = getMinioClient();
        loader = new TicketLoader();
        System.out.println("ImportBean() constructor completed");
    }

    protected ImportBean(Class<Import> classType, String name) {
        super(classType, name);
        loader = new TicketLoader();
    }
    private final Object transactionLock = new Object();


    public String commit(List<Ticket> tickets, UploadedFile file) throws Exception {
        System.out.println("commit started");
        boolean simulateError = false;
        String bucketName = "bucket";
        String fileName = currentImport.getOwner().getLogin() + "_" + System.currentTimeMillis() + "/" +
                Objects.requireNonNull(file.getFileName());

        EntityManager em = PersistenceManager.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        synchronized (transactionLock) {
            try {
                transaction.begin();
                System.out.println("Transaction started");
                try {
                    saveTickets(tickets, em);
                    System.out.println("tickets saved");
                    if (simulateError) {
                        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Какая-то ошибка в бизнес логике", null));
                        throw new RuntimeException("Какая-то ошибка в бизнес логике -> rollback транзакции");
                    }

                    InputStream fileInputStream = file.getInputStream();
                    long fileSize = file.getSize();

                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(fileName)
                                    .stream(fileInputStream, fileSize, -1)
                                    .build()
                    );

                    String fileUrlAfterSave = minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucketName)
                                    .object(fileName)
                                    .build());

                    currentImport.setFileUrl(fileUrlAfterSave);
                    currentImport.setStatus(ImportStatus.FINISHED);
                    currentImport.setCreatedEntitiesCount((long) tickets.size());
                    currentImport.setMessage("Import completed successfully");
                    Actions.addCommit(currentImport, em);

                    transaction.commit();
                    return fileUrlAfterSave;

                } catch (Exception e) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                    throw e;
                }

            } catch (Exception e) {
                throw e;

            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    public void importDataFromUpload() throws Exception {
        System.out.println("importDataFromUpload called");
        currentImport.setOwner(getCurrentOwner());
        currentImport.setStatus(ImportStatus.FAILED);
        try {
            if (xmlFile == null) {
                throw new Exception("No file uploaded!");
            }
            System.out.println("File uploaded");
            List<Ticket> tickets = loader.loadTicketsFromFile(xmlFile);

            String fileUrl = commit(tickets, xmlFile);

            currentImport.setFileUrl(fileUrl);
            currentImport.setStatus(ImportStatus.FINISHED);
            currentImport.setCreatedEntitiesCount((long) tickets.size());
            currentImport.setMessage("Import completed successfully");
            Actions.add(currentImport);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Import successful", null));
            System.out.println("Ticket saved successfully.");

        } catch (Exception e) {
            System.out.println("Import error: " + e);
            currentImport.setMessage(e.getMessage());
            Actions.add(currentImport);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Transaction failed", null));
        }
    }
    public void saveTickets(List<Ticket> tickets, EntityManager em) throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<Ticket> savedTickets = new ArrayList<>();
        Thread.sleep(5000);
        try {
            for (Ticket ticket : tickets) {
                ValidationResult valid = validateTicket(ticket);

                if (!valid.getIsValid()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Import failed due to invalid ticket data", null));
                    throw new Exception("Import failed due to invalid ticket data.");
                }
                try {
                    ticket.getVenue().getAddress().setOwner(getCurrentOwner());
                    Actions.addCommit(ticket.getVenue().getAddress(), em);

                    ticket.getVenue().setOwner(getCurrentOwner());
                    Actions.addCommit(ticket.getVenue(), em);

                    ticket.getCoordinates().setOwner(getCurrentOwner());
                    Actions.addCommit(ticket.getCoordinates(), em);

                    ticket.getEvent().setOwner(getCurrentOwner());
                    Actions.addCommit(ticket.getEvent(), em);

                    ticket.getPerson().getLocation().setOwner(getCurrentOwner());
                    Actions.addCommit(ticket.getPerson().getLocation(), em);

                    ticket.getPerson().setOwner(getCurrentOwner());
                    Actions.addCommit(ticket.getPerson(), em);

                    ticket.setOwner(getCurrentOwner());
                    Actions.addCommit(ticket, em);

                    savedTickets.add(ticket);
                } catch (Exception e) {
                    System.err.println("Error saving ticket: " + ticket.getName() + " - " + e.getMessage());
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error saving ticket: " + ticket.getName() + " - " + e.getMessage(), null));
                    throw e;
                }
            }

            em.flush();

        } catch (Exception e) {
            throw e;
        }
    }
    private boolean isTicketNameExists(String name) {
        List<Ticket> existingTickets = Actions.findAll(Ticket.class);
        return existingTickets.stream()
                .anyMatch(ticket -> ticket.getName().equals(name));
    }

    public ValidationResult validateTicket(Ticket ticket) {
        List<String> errors = new ArrayList<>();

        if (ticket.getPrice() <= 0) {
            errors.add("Price должен быть больше 0");
        }

        if (ticket.getName() == null || ticket.getName().isEmpty()) {
            errors.add("Name не может быть null, строка не может быть пустой");
        }

        if (ticket.getType() == null) {
            errors.add("TicketType не может быть null");
        }

        if (ticket.getDiscount() <= 0 || ticket.getDiscount() > 100) {
            errors.add("Discount должен быть больше 0, но меньше 100");
        }

        if (ticket.getNumber() <= 0) {
            errors.add("Number должен быть больше 0");
        }

        if (ticket.getVenue().getAddress() != null && ticket.getVenue().getAddress().getStreet() == null) {
            errors.add("Street не может быть null");
        }

        if (ticket.getVenue().getAddress() != null && ticket.getVenue().getAddress().getZipCode() == null) {
            errors.add("Zipcode не может быть null");
        } else if (ticket.getVenue().getAddress() != null && ticket.getVenue().getAddress().getZipCode().length() > 29) {
            errors.add("Zipcode не может быть длиннее 29 символов");
        }

        if (ticket.getCoordinates() != null) {
            if (ticket.getCoordinates().getX() == null) {
                errors.add("X не может быть null");
            } else if (ticket.getCoordinates().getX() > 755) {
                errors.add("X не может быть больше 755");
            }

            if (ticket.getCoordinates().getY() > 685) {
                errors.add("Y не может быть больше 685");
            }
        }

        if (ticket.getEvent().getName() == null || ticket.getEvent().getName().isEmpty()) {
            errors.add("Event name не может быть null, строка не может быть пустой");
        }

        if (ticket.getEvent().getTicketsCount() <= 0) {
            errors.add("TicketsCount должно быть больше нуля");
        }

        if (ticket.getPerson().getLocation().getName() == null || ticket.getPerson().getLocation().getName().isEmpty()) {
            errors.add("Person location name не может быть пустой");
        }

        if (ticket.getPerson().getHairColor() == null) {
            errors.add("Person hair color не может быть null");
        }

        if (ticket.getPerson().getHeight() <= 0) {
            errors.add("Person height должен быть больше нуля");
        }

        if (ticket.getVenue().getName() == null || ticket.getVenue().getName().isEmpty()) {
            errors.add("Venue name не может быть null, строка не может быть пустой");
        }

        if (ticket.getVenue().getCapacity() <= 0) {
            errors.add("Venue capacity должен быть больше нуля");
        }

        if (ticket.getVenue().getType() == null) {
            errors.add("Venue type не может быть null");
        }

        if (errors.isEmpty()) {
            return new ValidationResult(true, "");
        } else {
            return new ValidationResult(false, String.join(", ", errors));
        }
    }

    public List<Import> getImports() {
        if (user.isAdmin()) {
            return Actions.findAll(Import.class);
        } else {
            return Actions.getAllByUser(Import.class, user.getLogin());
        }
    }

    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
    }

    @Override
    public List<Long> getIdList() {
        return getItems().stream().map(Import::getId).collect(Collectors.toList());
    }

    @Override
    public void emptyInstance() {
        super.getItemsStack().push(new Import());
        super.getStackItem().setId(0L);
    }

    @Override
    public List<String> getFieldNames() {
        return List.of("id",
                "status",
                "message",
                "createdEntitiesCount"
        );
    }
}
