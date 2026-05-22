package com.snowball.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NovelDetailVO extends NovelVO {
    private List<NovelChapterVO> chapters;
}
