package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.PersonaLibrary;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {
    private final PersonaLibrary personaLibrary;
    public PersonaController(PersonaLibrary personaLibrary) { this.personaLibrary = personaLibrary; }
    @GetMapping
    public Map<String, Object> all() { return R.ok(personaLibrary.all()); }
}
