import random
from time import sleep

import scrapy
from scrapy.spiders import CrawlSpider
from selenium import webdriver
from WebCrawler.items import WebcrawlerItem
from selenium.webdriver.chrome.options import Options

class ChinaDailySpider(CrawlSpider):
    name = 'chinadaily_com_cn'
    base_url = 'http://global.chinadaily.com.cn/tech/'

    def __init__(self, *a, **kw):
        options = Options()
        # options.add_argument('--headless')
        # options.add_argument('--no-sandbox')
        # options.add_argument('--disable-dev-shm-usage')
        super().__init__(*a, **kw)
        self.model_urls = []  # 需要动态加载的url
        self.browser = webdriver.Chrome(
            executable_path='chromedriver.exe')

    def start_requests(self):
        for i in range(500):
            target_url = self.base_url + 'page_' + str(i + 1) + '.html'
            yield scrapy.Request(url=target_url, callback=self.parse)

    def parse(self, response):
        sleep(random.randrange(0,1))
        a_list = response.xpath('//div[@class="twBox_t1"]/a')
        for a in a_list:
            title = a.xpath('./text()').extract_first()
            url = 'https:' + a.xpath('./@href').extract_first()
            item = WebcrawlerItem()  # 实例化一个item对象
            item['language'] = 'EN'
            item['title'] = title  # 把抓取到的title放入到item对象中
            item['url'] = url

            # 依次对每一个板块对应的页面进行请求
            if url is not None:
                yield scrapy.Request(url=url, callback=self.parse_detail, meta={'item': item})

    def parse_detail(self, response):  # 解析新闻内容
        content = response.xpath('//*[@id="Content"]/p/text()').extract()
        # ''.join()把列表中的元素连接成一个字符串，strip()去掉换行，replace(" ", "")去掉看空格
        content = ''.join(content).strip()
        item = response.meta['item']
        item['content'] = content
        sleep(1)

        yield item  # 将item对象提交给piplelines（管道文件），用于持久化存储

    def closed(self, spider):
        self.browser.quit()  # 关闭模拟浏览器
