package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.services.BeerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerController {

    private final BeerService beerService;

    public static final String BEER_PATH = "/api/v1/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    @GetMapping(BEER_PATH)
    public List<BeerDTO> listBeer() {

        return beerService.listBeers();
    }

    @PostMapping(BEER_PATH)
    public ResponseEntity<BeerDTO> handlePost(@Validated @RequestBody BeerDTO beerDTO) {
        BeerDTO savedBeerDTO = beerService.saveNewBeer(beerDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", BEER_PATH + "/" + savedBeerDTO.getId().toString());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping(BEER_PATH_ID)
    public BeerDTO getBeerById (@PathVariable UUID beerId) {

        return beerService.getBeerById(beerId).orElseThrow(NotFoundException::new);
    }

    @PutMapping(BEER_PATH_ID)
    public ResponseEntity<BeerDTO> updateById(@PathVariable UUID beerId,@Validated @RequestBody BeerDTO beerDTO) {

        if (beerService.updateBeerById(beerId, beerDTO).isEmpty()) {
            throw new NotFoundException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", BEER_PATH + "/" + beerId.toString());

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @PatchMapping(BEER_PATH_ID)
    public ResponseEntity patchUpdateById(@PathVariable UUID beerId, @RequestBody BeerDTO beerDTO) {

        if (beerService.patchBeerById(beerId, beerDTO).isEmpty()){
            throw new NotFoundException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", BEER_PATH + "/" + beerDTO.toString());

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(BEER_PATH_ID)
    public ResponseEntity deleteById (@PathVariable UUID beerId) {
        log.debug("Deleting Beer by Id - " + beerId);

        if (!beerService.deleteById(beerId)){
            throw new NotFoundException();
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
