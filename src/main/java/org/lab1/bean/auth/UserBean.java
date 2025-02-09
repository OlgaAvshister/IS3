package org.lab1.bean.auth;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.lab1.data.Actions;
import org.lab1.data.entity.User;

import lombok.Data;

@SuppressWarnings("deprecation")
@ManagedBean(name = "userBean")
@SessionScoped
@Data
public class UserBean {
    private Long id = 0L;
    private String login;
    private String password;
    private boolean isAdmin = false;
    private boolean isNotMain = true;
    private User user;

    public boolean validateUser() {
        User probUser = Actions.getUserByLogin(login);
        System.out.println(probUser);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String errorMsg = "";

        if (probUser == null) {
            errorMsg = "Invalid login";
        } else {
            boolean cor = probUser.getPassword().equals(User.hashString(password));
            if (!cor) {
                errorMsg = "Invalid password";
            } else if (probUser.isNotMain()) {
                errorMsg = "Admin is checking your request";
            } else {
                errorMsg = "Logged in";
            }
        }
        facesContext.addMessage(":loginForm:loginPanel", new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, null));
        user = probUser;
        id = probUser.getId();
        isAdmin = probUser.isAdmin();
        return "Logged in".equals(errorMsg);
    }


    public User toEntity() {
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(password);
        newUser.setAdmin(isAdmin);
        newUser.setNotMain(isNotMain);
        return newUser;
    }

    public int logout() {
        id = 0L;
        return 1;
    }

    public boolean register() {
        User newUser = toEntity();
        try {
            Actions.addMain(newUser);
            return true;
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(":registerForm:registerPanel", new FacesMessage(FacesMessage.SEVERITY_ERROR, "login/password is empty or in used", null));
            return false;
        }
    }

}
