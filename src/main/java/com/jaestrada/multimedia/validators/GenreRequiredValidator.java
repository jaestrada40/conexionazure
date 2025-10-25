package com.jaestrada.multimedia.validators;

import com.jaestrada.multimedia.models.MovieGenre;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.util.List;

@FacesValidator("genreRequiredValidator")
public class GenreRequiredValidator implements Validator<List<MovieGenre>> {
    
    @Override
    public void validate(FacesContext context, UIComponent component, List<MovieGenre> value) 
            throws ValidatorException {
        
        if (value == null || value.isEmpty()) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Géneros requeridos",
                "Debe seleccionar al menos un género para el título"
            );
            throw new ValidatorException(message);
        }
    }
}