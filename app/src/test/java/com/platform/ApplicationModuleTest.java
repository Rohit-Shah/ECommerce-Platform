package com.platform;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;


public class ApplicationModuleTest {

    private final ApplicationModules modules = ApplicationModules.of(Application.class);

    @Test
    void verifyModularStructure(){
        modules.verify();
    }

    @Test
    void generateModuleDocumentation(){
        new Documenter(modules).writeDocumentation().writeIndividualModulesAsPlantUml();
    }

}
