from time import sleep

import scrapy
from lxml import etree
from scrapy.spiders import CrawlSpider, Rule
from selenium import webdriver
from WebCrawler.items import WebcrawlerItem


class News163ComSpider(CrawlSpider):
    name = 'news_163_com'

    def __init__(self, *a, **kw):
        super().__init__(*a, **kw)
        self.model_urls = []   # 需要动态加载的url
        self.browser = webdriver.Chrome(
            executable_path='chromedriver.exe')

    def start_requests(self):
        return [scrapy.FormRequest("https://news.163.com/",
                                   callback=self.parse)]

    def parse(self, response):
        li_list = response.xpath('//*[@id="index2016_wrap"]/div[1]/div[2]/div[2]/div[2]/div[2]/div/ul/li')
        model_list = [3, 4, 6, 7, 8]
        for index in model_list:
            model_url = li_list[index].xpath('./a/@href').extract_first()
            self.model_urls.append(model_url)

        # 依次对每一个板块对应的页面进行请求
        for url in self.model_urls:  # 对每一个板块的url进行请求发送
            yield scrapy.Request(url=url, callback=self.parse_model)

    # 每一个板块对应的新闻标题相关的内容都是动态加载
    def parse_model(self, response):  # 解析每一个板块页面中对应新闻的标题和新闻详情页的url
        self.browser.execute_script("window.scrollTo(0,document.body.scrollHeight);")  # 调用js动作，模拟下滑到底
        sleep(0.5)
        response = etree.HTML(self.browser.page_source)
        div_list = response.xpath('//div[@class="data_row news_article clearfix "]')
        for div in div_list:
            title = div.xpath('./div/div[1]/h3/a/text()')[0]
            new_detail_url = div.xpath('./div/div[1]/h3/a/@href')[0]
            item = WebcrawlerItem()  # 实例化一个item对象
            item['language'] = 'ZH'
            item['title'] = title  # 把抓取到的title放入到item对象中
            item['url'] = new_detail_url

            # 对新闻详情页的url发起请求，meta是一个字典，用来把item对象传出去
            if new_detail_url is not None:
                yield scrapy.Request(url=new_detail_url, callback=self.parse_detail, meta={'item': item})

    def parse_detail(self, response):  # 解析新闻内容
        content = response.xpath('//*[@class="post_body"]/p/text()').extract()
        # ''.join()把列表中的元素连接成一个字符串，strip()去掉换行，replace(" ", "")去掉空格
        content = ''.join(content).strip().replace(" ", "")
        item = response.meta['item']
        item['content'] = content
        sleep(1)

        yield item  # 将item对象提交给piplelines（管道文件），用于持久化存储

    def closed(self, spider):
        self.browser.quit()  # 关闭模拟浏览器
