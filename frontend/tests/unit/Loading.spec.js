import {describe,it,expect,vi} from "vitest";
import {mount} from "@vue/test-utils";
import Loading from "@/components/Loading.vue";

describe('Loading.vue',() => {
    it('初始状态不可见', () => {
        const wrapper = mount(Loading)
        expect(wrapper.vm.visible).toBe(false)
    });

    it('调用 show 方法后可见', () => {
        const wrapper = mount(Loading)
        wrapper.vm.show('加载中...')
        expect(wrapper.vm.visible).toBe(true)
        expect(wrapper.vm.text).toBe('加载中...')
    });

    it('调用 hide 方法后隐藏', () => {
        const wrapper = mount(Loading)
        wrapper.vm.show()
        expect(wrapper.vm.visible).toBe(true)

        wrapper.vm.hide()
        expect(wrapper.vm.visible).toBe(false)
    });
})