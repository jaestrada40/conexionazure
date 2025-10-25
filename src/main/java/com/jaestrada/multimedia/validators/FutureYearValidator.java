package com.jaestrada.multimedia.validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.time.LocalDateTime;

@FacesValidator("futureYearValidator")
public class FutureYearValidator implements Validator<Integer> {
    
    @Override
    public void validate(FacesContext context, UIComponent component, Integer value) 
            throws ValidatorException {
        
        if (value != null) {
            int currentYear = LocalDateTime.now().getYear();
            if (value > currentYear) {
                FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Año inválido",
                    "El año de lanzamiento no puede ser futuro. Año actual: " + currentYear
                );
                throw new ValidatorException(message);
            }
        }
    }
}