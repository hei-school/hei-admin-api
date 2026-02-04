package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.dto.SearchResults;
import school.hei.haapi.service.SearchService;

@RestController
@AllArgsConstructor
public class SearchController {
  private final SearchService searchService;

  @GetMapping("/global_search/user")
  public SearchResults globalSearch(@RequestParam(required = false) String search) {
    return searchService.searchAll(search);
  }
}
