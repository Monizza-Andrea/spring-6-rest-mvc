package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import guru.springframework.spring6restmvc.services.CustomerService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerRepository customerRepository;


    @Test
    void testGetCustomerList() {
        List<CustomerDTO> dtos = customerController.listCustomer();

        assertThat(dtos.size()).isEqualTo(3);
    }


    @Test
    void testGetCustomerById() {
        CustomerDTO customer = customerController.listCustomer().get(0);

        assertThat(customer).isNotNull();
    }


    @Rollback
    @Transactional
    @Test
    void testGetCustomerByIdNotFound() {
        CustomerDTO customer = customerController.listCustomer().get(0);

        customerRepository.deleteById(customer.getId());

        assertThrows(NotFoundException.class, () -> customerController.getCustomerById(customer.getId()));
    }


    @Transactional
    @Rollback
    @Test
    void testListAllEmptyList() {
        customerRepository.deleteAll();

        assertThat(customerController.listCustomer().size()).isEqualTo(0);
    }
}