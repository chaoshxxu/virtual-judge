package judge.tool;

import java.util.Comparator;

import judge.bean.Description;

public class DescriptionComparator implements Comparator<Description> {
    public int compare(Description o1, Description o2) {
        return o1.getUpdateTime().compareTo(o2.getUpdateTime()) < 0 ? -1 : 1;
    }
}
