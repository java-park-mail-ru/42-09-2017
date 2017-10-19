package ru.mail.park.controllers.validators;

import com.sun.istack.internal.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.mail.park.exceptions.ControllerValidationException;
import ru.mail.park.services.UserDao;

import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class CValidatorChain {
    private UserDao userDao;
    private ApplicationContext applicationContext;

    public CValidatorChain(UserDao userDao, ApplicationContext applicationContext) {
        this.userDao = userDao;
        this.applicationContext = applicationContext;
    }

    public <T> void validate(Object obj, HttpSession httpSession) {
        Method[] methods = obj.getClass().getMethods();
        List<String> responseList = new ArrayList<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(CValidator.class)) {
                final CValidator cValidator = method.getAnnotation(CValidator.class);
                Validator validator = (Validator) applicationContext.getBean(cValidator.fieldName() + "Validator");
                Object value = null;
                try {
                    value = method.invoke(obj);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                String response = validator.validate(value, httpSession, cValidator.nullable());
                if (response != null) {
                    responseList.add(response);
                }
            }
        }
        if (!responseList.isEmpty()) {
            throw new ControllerValidationException(responseList);
        }
    }
}
