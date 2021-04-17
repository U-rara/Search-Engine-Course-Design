import re
from time import sleep

import scrapy
from lxml import etree
from scrapy.spiders import CrawlSpider
from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, ElementClickInterceptedException
from selenium.webdriver.chrome.options import Options

from WebCrawler.items import WebcrawlerItem


class kr36Spider(CrawlSpider):
    name = '36kr_com'
    base_url = 'https://36kr.com'
    target_url = 'https://36kr.com/information/web_news/latest'

    def __init__(self, *a, **kw):
        super().__init__(*a, **kw)
        options = Options()
        # prefers = {"profile.managed_default_content_settings.images": 2, 'permissions.default.stylesheet': 2}
        # options.add_experimental_option("prefs", prefers)
        self.browser = webdriver.Chrome(
            executable_path='chromedriver.exe', chrome_options=options)
        self.model_urls = [self.target_url]

    def start_requests(self):
        return [scrapy.FormRequest(url=self.target_url,
                                   callback=self.parse)]

    def parse(self, response):
        # 板块对应的新闻标题相关的内容都是动态加载
        for j in range(5):
            for i in range(3):
                self.browser.execute_script("window.scrollTo(0,document.body.scrollHeight);")  # 调用js动作，模拟下滑到底
            while True:
                try:
                    try:
                        self.browser.find_element_by_xpath('//div[@class="kr-loading-more-button show"]').click()
                        break
                    except ElementClickInterceptedException:
                        sleep(0.5)
                except NoSuchElementException:
                    sleep(0.5)

        response = etree.HTML(self.browser.page_source)
        a_list = response.xpath('//a[@class="article-item-title weight-bold"]')
        for a in a_list:
            url = self.base_url + a.xpath('./@href')[0]
            title = a.xpath('./text()')[0]
            item = WebcrawlerItem()  # 实例化一个item对象
            item['language'] = 'ZH'
            item['title'] = title  # 把抓取到的title放入到item对象中
            item['url'] = url
            print('发现url：', url)
            # 对新闻详情页的url发起请求，meta是一个字典，用来把item对象传出去
            if url is not None:
                yield scrapy.Request(url=url, callback=self.parse_detail, meta={'item': item})

    def parse_detail(self, response):  # 解析新闻内容
        # print(response.body_as_unicode())
        content = response.xpath(
            '//*[@class="common-width content articleDetailContent kr-rich-text-wrapper"]/p/text()').extract()

        # ''.join()把列表中的元素连接成一个字符串，strip()去掉换行，replace(" ", "")去掉空格，splite()去掉换行符防止Typeerror
        try:
            content = ''.join(content).strip().replace(" ", "").split()[0]
        except IndexError:
            print('无有效内容，跳过')
        content = ''.join(content).strip().replace(" ", "").split()[0]
        content = re.sub(r'(https|http)?:\/\/(\w|\.|\/|\?|\=|\&|\%)*\b', '', content, flags=re.MULTILINE)  # 去掉正文中引用的图片
        content = re.sub(r'编者按.*发布。|编者按：', '', content)
        content = re.sub(r'\W+', '', content).replace("_", '')
        item = response.meta['item']
        item['content'] = content
        sleep(1)
        yield item  # 将item对象提交给piplelines（管道文件），用于持久化存储

    def closed(self, spider):
        self.browser.quit()  # 关闭模拟浏览器
