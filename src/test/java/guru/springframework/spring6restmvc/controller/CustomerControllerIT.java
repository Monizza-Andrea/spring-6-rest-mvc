package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import guru.springframework.spring6restmvc.services.CustomerService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerMapper customerMapper;


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

    @Rollback
    @Transactional
    @Test
    void updateExistingCustomer() {
        CustomerDTO customerDTO = customerController.listCustomer().get(0);
        UUID customerId = customerDTO.getId();
        customerDTO.setVersion(null);
        customerDTO.setId(null);
        final String name = "UPDATED CUSTOMER";
        customerDTO.setCustomerName(name);

        System.out.println(customerDTO.toString());
        ResponseEntity responseEntity = customerController.updateById(customerId, customerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        System.out.println(Arrays.toString(locationUUID));
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Customer customer = customerRepository.findById(savedUUID).get();
        assertThat(customer).isNotNull();
        assertThat(customer.getCustomerName()).isEqualTo(name);
    }

    @Test
    void testUpdateNotFound() {
        CustomerDTO customerDTO = CustomerDTO.builder().build();

        assertThrows(NotFoundException.class,() ->customerController.updateById(UUID.randomUUID(), customerDTO));
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteCustomerByID() {

        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customerRepository.findAll().get(0));

        ResponseEntity responseEntity = customerController.deleteById(customerDTO.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Optional<Customer> optionalCustomer = customerRepository.findById(customerDTO.getId());
        assertThat(optionalCustomer).isEqualTo(Optional.empty());
    }

    @Test
    void testDeleteByIdNotFound() {

        assertThrows(NotFoundException.class, () -> customerController.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testPatchExistinfCustomerById() {
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customerRepository.findAll().get(0));
        final String name = "UPDATED CUSTOMER NAME";
        customerDTO.setCustomerName(name);

        ResponseEntity responseEntity = customerController.patchById(customerDTO.getId(), customerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(customerRepository.findById(customerDTO.getId()).get().getCustomerName()).isEqualTo(name);
    }

    @Rollback
    @Transactional
    @Test
    void testPatchExistinfCustomerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.patchById(UUID.randomUUID(), CustomerDTO.builder().build()));
    }
}