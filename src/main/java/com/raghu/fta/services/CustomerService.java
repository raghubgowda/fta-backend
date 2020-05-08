package com.raghu.fta.services;

import com.raghu.fta.dao.CustomersRepo;
import com.raghu.fta.exceptions.EntityNotFoundException;
import com.raghu.fta.exceptions.ServiceException;
import com.raghu.fta.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CustomerService {
    private final CustomersRepo customersRepo;
    private static Logger logger = LoggerFactory.getLogger(CustomerService.class);

    public CustomerService(CustomersRepo customersRepo) {
        this.customersRepo = customersRepo;
    }

    public List<Customer> getCustomers(){
        List<Customer> customers;
        try{
            customers = customersRepo.findAll();
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the customers from the database. Error: "+e.getMessage());
        }
        return customers;
    }

    public Customer getCustomerById(String id){
        Customer customer;
        Optional<Customer> customerOptional;

        try{
            customerOptional = customersRepo.findById(id);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the customer with id "+id+". Error: "+e.getMessage());
        }

        if(customerOptional.isPresent()){
            customer = customerOptional.get();
        }
        else{
            throw new EntityNotFoundException("Customer with id "+id+" not found");
        }
        return customer;
    }

    public Customer updateCustomer(Customer customer){
        Customer updatedCustomer;

        //Check if the customer exists before updating it.
        getCustomerById(customer.getId());

        try{
            updatedCustomer = customersRepo.save(customer);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while saving the customer details. Error: "+e.getMessage());
        }
        return updatedCustomer;
    }

    public Customer saveCustomer(Customer customer){
        Customer savedCustomer;

        try{
            savedCustomer = customersRepo.save(customer);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while saving the customer details. Error: "+e.getMessage());
        }
        return savedCustomer;
    }

    public void delete(String id) {
        try{
            customersRepo.deleteById(id);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while deleting the customer with id "+id+". Error: "+e.getMessage());
        }
    }
}
