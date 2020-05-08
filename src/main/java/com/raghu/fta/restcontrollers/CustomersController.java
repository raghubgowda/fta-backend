package com.raghu.fta.restcontrollers;

import com.raghu.fta.model.Customer;
import com.raghu.fta.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
public class CustomersController {

    private CustomerService customerService;
    private static Logger logger = LoggerFactory.getLogger(CustomersController.class);

    public CustomersController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping()
    ResponseEntity<?> getCustomers(){
        logger.debug("Fetching all the customers");

        List<Customer> customers = customerService.getCustomers();

        logger.debug("Fetched all the customers");

        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @GetMapping("/{customerId}")
    ResponseEntity<?> getCustomerById(@PathVariable String customerId){
        logger.debug("Fetching the details for the Customer with id: {}", customerId);

        Customer customerById = customerService.getCustomerById(customerId);

        logger.debug("Customer with id: {} is found", customerById);

        return new ResponseEntity<>(customerById, HttpStatus.OK);
    }

    @PostMapping()
    ResponseEntity<?> addCustomer(@RequestBody Customer customer){
        logger.debug("Adding customer: {}", customer);

        if(customer.getId() != null){
            return new ResponseEntity<>("Customer id should be null", HttpStatus.BAD_REQUEST);
        }

        Customer newCustomer = customerService.saveCustomer(customer);

        logger.debug("Customer added successfully {}", newCustomer);

        return new ResponseEntity<>("Customer added successfully", HttpStatus.OK);
    }

    @PutMapping()
    ResponseEntity<?> updateCustomer(@RequestBody Customer customer){
        logger.debug("Updating the customer: {}", customer);

        if(customer.getId() == null || Strings.isEmpty(customer.getId())){
            return new ResponseEntity<>("Customer id cannot be null or empty", HttpStatus.BAD_REQUEST);
        }

        customerService.updateCustomer(customer);

        logger.debug("Customer updated successfully");

        return new ResponseEntity<>("Customer updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{customerId}")
    ResponseEntity<?> deleteCustomer(@PathVariable String customerId){
        logger.debug("Deleting the customer with id: {}", customerId);

        customerService.delete(customerId);

        logger.debug("Customer with id: {} deleted successfully", customerId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
