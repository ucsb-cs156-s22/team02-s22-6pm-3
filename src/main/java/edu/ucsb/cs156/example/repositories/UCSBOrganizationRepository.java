//package main.java.edu.ucsb.cs156.example.repositories;
package edu.ucsb.cs156.example.repositories;

import /*main.java.*/edu.ucsb.cs156.example.entities.UCSBOrganization;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UCSBOrganizationRepository extends CrudRepository<UCSBOrganization, String> {
    //  Iterable<UCSBDate> findAllByQuarterYYYYQ(String quarterYYYYQ);//from date repository
    //Iterable<UCSBOrganization> findAllByID(String ID);
}