import web
import audio

urls = (
    '/', 'index',
    '/tag/(.+)', 'tag'
)

class index:
    def GET(self):
        print "index access"
        return "hello world!"
    
    def POST(self):
        return "hello world!"

class tag:
    def GET(self, name):
        return "hello world " + str(name)
    
    def POST(self, name):
        destination_path = "uploads/test_track.mp3"
        data = web.input()
               
        fout = open(destination_path, 'w')
        fout.write(data['uploadedfile'])
        fout.close()
        
        result = audio.classify(destination_path)[0][1].split('_')[0]
        
        return result

if __name__ == "__main__":
    app = web.application(urls, globals())
    app.internalerror = web.debugerror
    app.run()