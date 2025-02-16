package org.lab1.bean.data.abstracts;

import lombok.Data;
import org.lab1.bean.auth.UserBean;
import org.lab1.bean.data.Identable;
import org.lab1.bean.data.TicketBean;
import org.lab1.context.MyException;
import org.lab1.data.Actions;
import org.lab1.data.entity.Ownerable;
import org.lab1.data.entity.Ticket;
import org.lab1.data.entity.User;
import org.postgresql.util.PSQLException;
import org.primefaces.PrimeFaces;

import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import static org.lab1.data.Actions.find;

@SuppressWarnings("deprecation")
@ManagedBean(name = "managerBean")
@SessionScoped
@Data
public abstract class ManagerBean<T extends Ownerable & Identable> {
    protected final Class<T> classType;
    protected final String editPanelName;
    protected final String componentDialogName;
    protected final String name;
    private static final Logger logger = Logger.getLogger(ManagerBean.class.getName());

    protected Stack<T> itemsStack;

    protected T getStackItem() {
        return itemsStack.peek();
    }

    private void protectedInit() {
        emptyInstance();
    }

    protected ManagerBean(Class<T> classType, String name) {
        this.editPanelName = name + "EditPanel";
        this.componentDialogName = name + "ComponentDialog";
        this.name = name;
        this.itemsStack = new Stack<>();
        this.classType = classType;

        protectedInit();
    }

    public void setSelectedItem(T selectedItem) {
        itemsStack.push(selectedItem);
    }

    public abstract List<Long> getIdList();

    public abstract void emptyInstance();

    public void addItem() {
        T stackItem = itemsStack.pop();
        if (stackItem.getOwner() == null)
            stackItem.setOwner(getCurrentOwner());
        Actions.add(stackItem);
    }

    protected User getCurrentOwner() {
        Map<String, Object> session = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return find(User.class, ((UserBean) session.get("userBean")).getId());
    }


    public void editStack() {
        if ((!itemsStack.isEmpty()) && (itemsStack.peek().getId() > 0)) {
            PrimeFaces.current().ajax().update(editPanelName);
            PrimeFaces.current().executeScript("PF('" + componentDialogName + "').show()");
        }
    }

    public List<T> getItems() {
        return Actions.findAll(classType);
    }

    public void editItem()  {
        T stackItem = itemsStack.pop();
        if (stackItem.getOwner() == null)
            stackItem.setOwner(getCurrentOwner());
        Actions.update(stackItem);
        editStack();
    }

    public void freeStack() {
        itemsStack.clear();
        emptyInstance();
    }

    public void removeItem() throws PSQLException {
        T stackItem = itemsStack.pop();
        if (stackItem.getOwner() == null)
            stackItem.setOwner(getCurrentOwner());
        Actions.delete(stackItem);
        editStack();
    }

    public abstract List<String> getFieldNames();

    public Object getFieldValue(T item, String fieldName) {
        try {
            Field field = this.classType.getDeclaredField(fieldName);
            return field.get(item);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MyException("Runtime exception", e);
        }
    }
}



