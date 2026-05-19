import {describe,it,expect,vi} from "vitest";
import {downloadFile} from '@/utils/export'

describe('export.js',() => {
    it('downloadFile 创建下载链接', () => {
        const createElement = vi.spyOn(document,'createElement')
        const appendChild = vi.spyOn(document.body,'appendChild')
        const removeChild = vi.spyOn(document.body,'removeChild')

        downloadFile('测试内容','test.txt','text/plain')

        expect(createElement).toHaveBeenCalledWith('a')
        expect(appendChild).toHaveBeenCalled()
        expect(removeChild).toHaveBeenCalled()
    });
})