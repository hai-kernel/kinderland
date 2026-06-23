import React from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { blogs } from '../../data/blogs';
import { Calendar, Clock, User, ArrowLeft, Share2, BookmarkPlus } from 'lucide-react';

export default function BlogDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const blog = blogs.find(b => b.id === id);

  if (!blog) {
    return (
      <div className="container mx-auto px-4 py-20 text-center">
        <p className="text-gray-500 text-lg">Không tìm thấy bài viết</p>
      </div>
    );
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric' 
    });
  };

  const relatedBlogs = blogs
    .filter(b => b.category === blog.category && b.id !== blog.id)
    .slice(0, 3);

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="container mx-auto px-4 py-8">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-[#78A2D2] hover:text-[#6A94C4] mb-6 font-semibold"
        >
          <ArrowLeft className="size-5" />
          Quay lại
        </button>

        {/* Blog Content */}
        <article className="max-w-4xl mx-auto">
          {/* Header */}
          <div className="bg-white rounded-3xl shadow-xl overflow-hidden mb-8 border-2 border-gray-200">
            <div className="aspect-video overflow-hidden">
              <img
                src={blog.image}
                alt={blog.title}
                className="w-full h-full object-cover"
              />
            </div>
            <div className="p-8 md:p-12">
              <div className="inline-block bg-[#78A2D2]/20 text-[#78A2D2] px-4 py-2 rounded-full text-sm font-semibold mb-4">
                {blog.category}
              </div>
              <h1 className="text-4xl md:text-5xl font-bold text-gray-800 mb-6">
                {blog.title}
              </h1>
              
              {/* Meta Info */}
              <div className="flex flex-wrap items-center gap-6 text-gray-600 mb-6 pb-6 border-b border-gray-200">
                <div className="flex items-center gap-2">
                  <User className="size-5" />
                  <span className="font-semibold">{blog.author}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Calendar className="size-5" />
                  <span>{formatDate(blog.date)}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="size-5" />
                  <span>{blog.readTime} đọc</span>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 mb-8">
                <button className="flex items-center gap-2 px-6 py-3 bg-[#78A2D2] text-white rounded-xl hover:bg-[#6A94C4] transition-all font-semibold">
                  <Share2 className="size-5" />
                  Chia sẻ
                </button>
                <button className="flex items-center gap-2 px-6 py-3 border-2 border-[#78A2D2] text-[#78A2D2] rounded-xl hover:bg-[#78A2D2]/10 transition-all font-semibold">
                  <BookmarkPlus className="size-5" />
                  Lưu bài
                </button>
              </div>

              {/* Excerpt */}
              <div className="bg-[#FEFFAF]/30 border-l-4 border-[#78A2D2] p-6 rounded-lg mb-8">
                <p className="text-lg text-gray-700 font-medium italic">
                  {blog.excerpt}
                </p>
              </div>

              {/* Content */}
              <div className="prose prose-lg max-w-none">
                <p className="text-gray-700 leading-relaxed whitespace-pre-line">
                  {blog.content}
                </p>
                
                <h2 className="text-2xl font-bold text-gray-800 mt-8 mb-4">
                  Những điều cần lưu ý
                </h2>
                <ul className="space-y-2 text-gray-700">
                  <li>Luôn kiểm tra độ tuổi phù hợp trên bao bì đồ chơi</li>
                  <li>Chọn sản phẩm có nguồn gốc xuất xứ rõ ràng</li>
                  <li>Theo dõi bé khi chơi để đảm bảo an toàn</li>
                  <li>Vệ sinh đồ chơi định kỳ để bảo vệ sức khỏe của bé</li>
                </ul>

                <h2 className="text-2xl font-bold text-gray-800 mt-8 mb-4">
                  Kết luận
                </h2>
                <p className="text-gray-700 leading-relaxed">
                  Việc lựa chọn đồ chơi phù hợp không chỉ mang lại niềm vui cho trẻ mà còn góp phần quan trọng vào sự phát triển toàn diện của các bé. Hãy luôn đặt an toàn và sự phát triển của trẻ lên hàng đầu khi lựa chọn đồ chơi.
                </p>
              </div>

              {/* Tags */}
              <div className="mt-8 pt-8 border-t border-gray-200">
                <h3 className="font-bold text-gray-800 mb-3">Tags:</h3>
                <div className="flex flex-wrap gap-2">
                  {['đồ chơi', 'trẻ em', 'phát triển', 'giáo dục', 'an toàn'].map(tag => (
                    <span
                      key={tag}
                      className="px-4 py-2 bg-gray-100 text-gray-700 rounded-full text-sm hover:bg-[#78A2D2]/10 hover:text-[#78A2D2] transition-colors cursor-pointer"
                    >
                      #{tag}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Related Blogs */}
          {relatedBlogs.length > 0 && (
            <div className="mt-12">
              <h2 className="text-3xl font-bold text-gray-800 mb-6">Bài viết liên quan</h2>
              <div className="grid md:grid-cols-3 gap-6">
                {relatedBlogs.map((relatedBlog) => (
                  <Link
                    key={relatedBlog.id}
                    to={`/blog/${relatedBlog.id}`}
                    className="bg-white rounded-2xl shadow-lg overflow-hidden hover:shadow-xl transition-all group border-2 border-gray-200 hover:border-[#78A2D2]"
                  >
                    <div className="aspect-video overflow-hidden">
                      <img
                        src={relatedBlog.image}
                        alt={relatedBlog.title}
                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                      />
                    </div>
                    <div className="p-4">
                      <div className="inline-block bg-[#78A2D2]/20 text-[#78A2D2] px-3 py-1 rounded-full text-xs font-semibold mb-2">
                        {relatedBlog.category}
                      </div>
                      <h3 className="font-bold text-gray-800 line-clamp-2 group-hover:text-[#78A2D2] transition-colors">
                        {relatedBlog.title}
                      </h3>
                      <p className="text-sm text-gray-500 mt-2">
                        {relatedBlog.readTime}
                      </p>
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </article>
      </div>
    </div>
  );
}