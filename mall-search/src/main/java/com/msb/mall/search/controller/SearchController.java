package com.msb.mall.search.controller;

import com.msb.mall.search.service.MallSearchService;
import com.msb.mall.search.vo.SearchParam;
import com.msb.mall.search.vo.SearchResult;
import org.elasticsearch.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService searchService;

    /**
     * 商品检索处理
     * @param param
     * @return
     */

    @GetMapping(value = {"/list.html","index.html","/"})
    public String listPage(SearchParam param, Model model) {
        SearchResult result = searchService.search(param);
        model.addAttribute("result",result);
        return "index";
    }
}
