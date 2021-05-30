import scrapy
from scrapy.cmdline import execute
from scrapy.utils.project import get_project_settings
from scrapy.crawler import CrawlerProcess
# execute(['scrapy', 'crawl', 'news_163_com'])
# execute(['scrapy', 'crawl', '36kr_com'])
# execute(['scrapy', 'crawl', 'chinadaily_com_cn'])

if __name__ == '__main__':
    setting = get_project_settings()
    process = CrawlerProcess(setting)
    for spider_name in ['chinadaily_com_cn']:
        print("Running spider %s" % (spider_name))
        process.crawl(spider_name)
    process.start()
