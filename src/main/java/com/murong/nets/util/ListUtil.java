package com.murong.nets.util;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/05/14 09:59
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ListUtil {

    public static <T> List<T> retain(List<T> list1, List<T> list2) {
        if (list1 == null || list2 == null) {
            return Lists.newArrayList();
        }
        List<T> ts = new ArrayList<>(list1);
        ts.retainAll(list2);
        return ts;
    }

}
