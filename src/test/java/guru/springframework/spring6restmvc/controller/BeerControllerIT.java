package guru.springframework.spring6restmvc.controller;

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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;


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