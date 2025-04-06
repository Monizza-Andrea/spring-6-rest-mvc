package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import static org.hamcrest.core.Is.is;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }


    @Test
    void testPatchBeerBadName() throws Exception {
        Beer beer = beerRepository.findAll().get(0);

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546Name163516565151165189646516546318964541651546");

        MvcResult mvcResult = mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }


    @Test
    void testListBeers() {
        List<BeerDTO> dtos = beerController.listBeer();

        assertThat(dtos.size()).isEqualTo(3);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyBeers() {

        beerRepository.deleteAll();
        List<BeerDTO> dtos = beerController.listBeer();

        assertThat(dtos.size()).isEqualTo(0);
    }

    @Test
    void testGetById() {
        Beer beer = beerRepository.findAll().get(0);

        BeerDTO dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void testGetByIdNotFound() {
        Beer beer = beerRepository.findAll().get(0);

        beerRepository.deleteById(beer.getId());


        assertThrows(NotFoundException.class, () -> beerController.getBeerById(beer.getId()));

    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewBeer() {
        BeerDTO beerDTO = BeerDTO.builder().beerName("New Beer").build();

        ResponseEntity responseEntity = beerController.handlePost(beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        System.out.println(Arrays.toString(locationUUID));

        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Beer beer = beerRepository.findById(savedUUID).get();
        assertThat(beer).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        BeerDTO beerDTO = beerController.listBeer().get(0);
        UUID beerId = beerDTO.getId();
        beerDTO.setVersion(null);
        beerDTO.setId(null);
        final String name = "UPDATED BEER";
        beerDTO.setBeerName(name);

        System.out.println(beerDTO.toString());
        ResponseEntity responseEntity = beerController.updateById(beerId, beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        System.out.println(Arrays.toString(locationUUID));
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Beer beer = beerRepository.findById(savedUUID).get();
        assertThat(beer).isNotNull();
        assertThat(beer.getBeerName()).isEqualTo(name);
    }

    @Test
    void testUpdateNotFound() {
        BeerDTO beerDTO = BeerDTO.builder().beerName("New Beer").build();

        assertThrows(NotFoundException.class, () -> beerController.updateById(UUID.randomUUID(), beerDTO));
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteBeerById() {
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beerRepository.findAll().get(0));

        ResponseEntity responseEntity = beerController.deleteById(beerDTO.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Optional<Beer> optionalBeerDTO = beerRepository.findById(beerDTO.getId());
        assertThat(optionalBeerDTO).isEqualTo(Optional.empty());
//        assertThat(beerRepository.findById(beerDTO.getId()).isEmpty());
    }

    @Test
    void testDeleteByIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testPatchExistingBeerById() {
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beerRepository.findAll().get(0));
        final String name = "UPDATED BEER NAME";
        beerDTO.setBeerName(name);

        ResponseEntity responseEntity = beerController.patchUpdateById(beerDTO.getId(), beerDTO);
        System.out.println(beerDTO);
        System.out.println(beerRepository.findById(beerDTO.getId()));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(beerRepository.findById(beerDTO.getId()).get().getBeerName()).isEqualTo(name);
    }

    @Rollback
    @Transactional
    @Test
    void testPatchExistingBeerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.patchUpdateById(UUID.randomUUID(), BeerDTO.builder().build()));
    }
}