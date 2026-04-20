import {describe,it,expect} from "vitest";
import {mount} from "@vue/test-utils";
import Skeleton from "@/components/Skeleton.vue";

describe('Skeleton.vue',() => {
    it('默认显示5个骨架项', () => {
        const wrapper = mount(Skeleton)
        const items = wrapper.findAll('.skeleton-item')
        expect(items.length).toBe(5)
    });

    it('可以通过 count 属性设置数量', () => {
        const wrapper = mount(Skeleton,{
            props:{ count:3 }
        })
        const items =wrapper.findAll('.skeleton-item')
        expect(items.length).toBe(3)
    });
})