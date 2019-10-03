package com.organization.Service;

import com.organization.Repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DesignationService {

    @Autowired
    DesignationRepository designationRepository;
}
