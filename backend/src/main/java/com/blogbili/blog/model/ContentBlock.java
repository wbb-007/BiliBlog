package com.blogbili.blog.model;

import java.util.List;

public record ContentBlock(String type, String content, List<String> items) {
}
