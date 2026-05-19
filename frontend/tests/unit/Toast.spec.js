import { describe, it, expect,vi} from "vitest";
import {mount} from "@vue/test-utils";
import Toast1 from "@/components/Toast.vue";
import Toast from "@/components/Toast.vue";

describe('Toast.vue', () => {
    it('初始状态不可见', () => {
        const wrapper = mount(Toast)
        expect(wrapper.vm.visible).toBe(false)
    });

    it('调用show方法后可见',async () => {
        const wrapper = mount(Toast)
        wrapper.vm.show('测试消息','info')
        expect(wrapper.vm.visible).toBe(true)
        expect(wrapper.vm.message).toBe('测试消息')
        expect(wrapper.vm.type).toBe('info')
    });

    it('3秒后自动隐藏',async () => {
        vi.useFakeTimers()
        const wrapper = mount(Toast)
        wrapper.vm.show('测试','info',3000)

        expect(wrapper.vm.visible).toBe(true)

        vi.advanceTimersByTime(3000)
        expect(wrapper.vm.visible).toBe(false)

        vi.useRealTimers()

    });
})